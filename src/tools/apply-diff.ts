import * as fs from 'fs';
import * as path from 'path';

/**
 * Parameters for the apply_diff tool
 */
export interface ApplyDiffParams {
  /** Path to the file to modify */
  path: string;
  /** Unified diff format string to apply */
  diff: string;
  /** If true, preview changes without modifying the file */
  dryRun?: boolean;
}

/**
 * Result of applying a diff
 */
export interface ApplyDiffResult {
  /** Whether the operation was successful */
  success: boolean;
  /** Original file content */
  originalContent: string;
  /** Modified file content (if successful) */
  modifiedContent?: string;
  /** Error message if operation failed */
  error?: string;
  /** Number of hunks applied */
  hunksApplied?: number;
}

/**
 * Represents a hunk in a unified diff
 */
interface DiffHunk {
  oldStart: number;
  oldCount: number;
  newStart: number;
  newCount: number;
  lines: string[];
}

/**
 * Parse a unified diff format
 */
function parseUnifiedDiff(diff: string): DiffHunk[] {
  const hunks: DiffHunk[] = [];
  const lines = diff.split('\n');
  let i = 0;
  
  while (i < lines.length) {
    const line = lines[i];
    
    // Look for hunk header: @@ -oldStart,oldCount +newStart,newCount @@
    const hunkMatch = line.match(/^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@/);
    
    if (hunkMatch) {
      const oldStart = parseInt(hunkMatch[1], 10);
      const oldCount = hunkMatch[2] ? parseInt(hunkMatch[2], 10) : 1;
      const newStart = parseInt(hunkMatch[3], 10);
      const newCount = hunkMatch[4] ? parseInt(hunkMatch[4], 10) : 1;
      
      i++;
      const hunkLines: string[] = [];
      
      // Collect lines until next hunk or end
      while (i < lines.length && !lines[i].startsWith('@@')) {
        hunkLines.push(lines[i]);
        i++;
      }
      
      hunks.push({
        oldStart,
        oldCount,
        newStart,
        newCount,
        lines: hunkLines
      });
    } else {
      i++;
    }
  }
  
  return hunks;
}

/**
 * Apply a single hunk to content
 */
function applyHunk(lines: string[], hunk: DiffHunk): string[] {
  const result: string[] = [];
  let lineIndex = 0;
  
  // Copy lines before the hunk
  while (lineIndex < hunk.oldStart - 1) {
    result.push(lines[lineIndex]);
    lineIndex++;
  }
  
  // Apply the hunk
  for (const hunkLine of hunk.lines) {
    if (hunkLine.startsWith('-')) {
      // Remove line
      lineIndex++;
    } else if (hunkLine.startsWith('+')) {
      // Add line
      result.push(hunkLine.substring(1));
    } else if (hunkLine.startsWith(' ')) {
      // Context line - keep it
      result.push(lines[lineIndex]);
      lineIndex++;
    }
  }
  
  // Copy remaining lines
  while (lineIndex < lines.length) {
    result.push(lines[lineIndex]);
    lineIndex++;
  }
  
  return result;
}

/**
 * Apply a unified diff to a file
 */
export async function applyDiff(params: ApplyDiffParams): Promise<ApplyDiffResult> {
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
    const lines = originalContent.split('\n');
    
    // Parse diff
    const hunks = parseUnifiedDiff(params.diff);
    
    if (hunks.length === 0) {
      return {
        success: false,
        originalContent,
        error: 'No valid hunks found in diff'
      };
    }
    
    // Apply hunks in reverse order to maintain line numbers
    let modifiedLines = [...lines];
    for (let i = hunks.length - 1; i >= 0; i--) {
      modifiedLines = applyHunk(modifiedLines, hunks[i]);
    }
    
    const modifiedContent = modifiedLines.join('\n');
    
    // Write to file if not dry run
    if (!params.dryRun) {
      fs.writeFileSync(absolutePath, modifiedContent, 'utf-8');
    }
    
    return {
      success: true,
      originalContent,
      modifiedContent,
      hunksApplied: hunks.length
    };
    
  } catch (error) {
    return {
      success: false,
      originalContent: fs.existsSync(params.path) 
        ? fs.readFileSync(params.path, 'utf-8') 
        : '',
      error: error instanceof Error ? error.message : String(error)
    };
  }
}
