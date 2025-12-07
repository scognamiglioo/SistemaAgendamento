import * as fs from 'fs';
import * as path from 'path';

/**
 * Represents a single edit operation with search and replace
 */
export interface EditOperation {
  /** Exact text to find in the file */
  search: string;
  /** Text to replace the found text with */
  replace: string;
  /** Optional description of what this edit does */
  description?: string;
}

/**
 * Parameters for the edit_file tool
 */
export interface EditFileParams {
  /** Path to the file to edit */
  path: string;
  /** Array of edit operations to apply atomically */
  edits: EditOperation[];
  /** If true, preview changes without modifying the file */
  dryRun?: boolean;
}

/**
 * Result of an edit operation
 */
export interface EditResult {
  /** Whether the operation was successful */
  success: boolean;
  /** Original file content */
  originalContent: string;
  /** Modified file content (if successful) */
  modifiedContent?: string;
  /** Error message if operation failed */
  error?: string;
  /** Details about what was changed */
  changes?: ChangeDetail[];
}

/**
 * Details about a specific change made
 */
export interface ChangeDetail {
  /** The edit operation that was applied */
  edit: EditOperation;
  /** Position where the replacement occurred */
  position: number;
  /** Number of lines affected */
  linesAffected: number;
}

/**
 * Error thrown when a search pattern validation fails
 */
export class SearchPatternError extends Error {
  constructor(
    message: string,
    public readonly pattern: string,
    public readonly context?: string
  ) {
    super(message);
    this.name = 'SearchPatternError';
  }
}

/**
 * Find all occurrences of a pattern in content
 */
function findAllOccurrences(content: string, pattern: string): number[] {
  // Empty pattern would match at every position - treat as no match
  if (pattern.length === 0) {
    return [];
  }
  
  const positions: number[] = [];
  let index = content.indexOf(pattern);
  
  while (index !== -1) {
    positions.push(index);
    index = content.indexOf(pattern, index + 1);
  }
  
  return positions;
}

/**
 * Get surrounding context for an error message
 */
function getSurroundingContext(content: string, position: number, contextLines: number = 3): string {
  const lines = content.split('\n');
  let charCount = 0;
  let lineIndex = 0;
  
  // Find which line the position is on
  for (let i = 0; i < lines.length; i++) {
    if (charCount + lines[i].length >= position) {
      lineIndex = i;
      break;
    }
    charCount += lines[i].length + 1; // +1 for newline
  }
  
  const startLine = Math.max(0, lineIndex - contextLines);
  const endLine = Math.min(lines.length - 1, lineIndex + contextLines);
  
  const contextLines_arr: string[] = [];
  for (let i = startLine; i <= endLine; i++) {
    const marker = i === lineIndex ? '> ' : '  ';
    contextLines_arr.push(`${marker}${i + 1}: ${lines[i]}`);
  }
  
  return contextLines_arr.join('\n');
}

/**
 * Find similar patterns in content (for helpful error messages)
 */
function findSimilarPattern(content: string, pattern: string): number | null {
  // Try to find a similar pattern (case-insensitive)
  const lowerContent = content.toLowerCase();
  const lowerPattern = pattern.toLowerCase();
  const index = lowerContent.indexOf(lowerPattern);
  
  if (index !== -1) {
    return index;
  }
  
  // Try without leading/trailing whitespace
  const trimmedPattern = pattern.trim();
  if (trimmedPattern !== pattern) {
    const trimIndex = content.indexOf(trimmedPattern);
    if (trimIndex !== -1) {
      return trimIndex;
    }
  }
  
  return null;
}

/**
 * Validate that all search patterns can be found exactly once
 */
function validateSearchPatterns(content: string, edits: EditOperation[]): void {
  for (let i = 0; i < edits.length; i++) {
    const edit = edits[i];
    const occurrences = findAllOccurrences(content, edit.search);
    
    if (occurrences.length === 0) {
      // Search pattern not found - provide helpful context
      const similarPos = findSimilarPattern(content, edit.search);
      let errorMessage = `Search pattern not found: "${edit.search}"`;
      
      if (similarPos !== null) {
        const context = getSurroundingContext(content, similarPos);
        errorMessage += `\n\nFound similar text (check for whitespace/case differences):\n${context}`;
      }
      
      errorMessage += `\n\nConsider checking:
- Whitespace differences (spaces, tabs, newlines)
- Line ending characters (\\r\\n vs \\n)
- Exact string matching (case-sensitive)`;
      
      throw new SearchPatternError(errorMessage, edit.search);
    }
    
    if (occurrences.length > 1) {
      const contexts = occurrences.slice(0, 3).map(pos => 
        getSurroundingContext(content, pos, 1)
      );
      
      const errorMessage = `Search pattern appears ${occurrences.length} times (must be unique): "${edit.search}"

Found at positions: ${occurrences.join(', ')}

First ${Math.min(3, occurrences.length)} occurrence(s):
${contexts.join('\n---\n')}

Make the search pattern more specific to match only the intended location.`;
      
      throw new SearchPatternError(errorMessage, edit.search);
    }
  }
}

/**
 * Check if any edit operations would overlap
 */
function checkForOverlaps(positions: Array<{ index: number; edit: EditOperation }>): void {
  // Sort by position
  const sorted = [...positions].sort((a, b) => a.index - b.index);
  
  for (let i = 0; i < sorted.length - 1; i++) {
    const current = sorted[i];
    const next = sorted[i + 1];
    const currentEnd = current.index + current.edit.search.length;
    
    if (currentEnd > next.index) {
      throw new SearchPatternError(
        `Edit operations overlap:\n` +
        `  1. "${current.edit.search}" at position ${current.index}\n` +
        `  2. "${next.edit.search}" at position ${next.index}\n\n` +
        `These patterns overlap in the file and cannot be applied atomically.`,
        current.edit.search
      );
    }
  }
}

/**
 * Count lines in a string
 */
function countLines(text: string): number {
  return (text.match(/\n/g) || []).length;
}

/**
 * Apply all edits to the content
 */
function applyEdits(content: string, edits: EditOperation[]): { result: string; changes: ChangeDetail[] } {
  // Find positions for all edits
  const positions = edits.map(edit => ({
    index: content.indexOf(edit.search),
    edit
  }));
  
  // Check for overlaps
  checkForOverlaps(positions);
  
  // Sort by position in reverse order to maintain indices
  positions.sort((a, b) => b.index - a.index);
  
  // Apply replacements from end to start
  let result = content;
  const changes: ChangeDetail[] = [];
  
  for (const { index, edit } of positions) {
    const before = result.substring(0, index);
    const after = result.substring(index + edit.search.length);
    result = before + edit.replace + after;
    
    changes.push({
      edit,
      position: index,
      linesAffected: Math.max(
        countLines(edit.search),
        countLines(edit.replace)
      )
    });
  }
  
  // Reverse changes to show them in original order
  changes.reverse();
  
  return { result, changes };
}

/**
 * Main function to edit a file with search/replace operations
 */
export async function editFile(params: EditFileParams): Promise<EditResult> {
  try {
    // Validate path
    const absolutePath = path.resolve(params.path);
    
    if (!fs.existsSync(absolutePath)) {
      return {
        success: false,
        originalContent: '',
        error: `File not found: ${params.path}`
      };
    }
    
    // Read file content
    const originalContent = fs.readFileSync(absolutePath, 'utf-8');
    
    // Validate that file is not empty if we have edits
    if (originalContent.length === 0 && params.edits.length > 0) {
      return {
        success: false,
        originalContent: '',
        error: 'Cannot apply edits to an empty file'
      };
    }
    
    // Validate all search patterns
    validateSearchPatterns(originalContent, params.edits);
    
    // Apply edits
    const { result: modifiedContent, changes } = applyEdits(originalContent, params.edits);
    
    // Write to file if not dry run
    if (!params.dryRun) {
      fs.writeFileSync(absolutePath, modifiedContent, 'utf-8');
    }
    
    return {
      success: true,
      originalContent,
      modifiedContent,
      changes
    };
    
  } catch (error) {
    if (error instanceof SearchPatternError) {
      return {
        success: false,
        originalContent: fs.existsSync(params.path) 
          ? fs.readFileSync(params.path, 'utf-8') 
          : '',
        error: error.message
      };
    }
    
    return {
      success: false,
      originalContent: '',
      error: error instanceof Error ? error.message : String(error)
    };
  }
}

/**
 * Generate a diff preview of changes
 */
export function generateDiffPreview(result: EditResult): string {
  if (!result.success || !result.modifiedContent) {
    return result.error || 'No changes';
  }
  
  const lines: string[] = [];
  lines.push('Changes to be applied:');
  lines.push('');
  
  if (result.changes) {
    for (const change of result.changes) {
      if (change.edit.description) {
        lines.push(`# ${change.edit.description}`);
      }
      lines.push(`- ${change.edit.search}`);
      lines.push(`+ ${change.edit.replace}`);
      lines.push('');
    }
  }
  
  return lines.join('\n');
}
