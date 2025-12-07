import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { editFile, EditFileParams, generateDiffPreview } from '../tools/edit-file';

describe('edit-file', () => {
  let tempDir: string;
  let testFilePath: string;

  beforeEach(() => {
    // Create a temporary directory for test files
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'edit-file-test-'));
    testFilePath = path.join(tempDir, 'test.txt');
  });

  afterEach(() => {
    // Clean up temporary directory
    if (fs.existsSync(tempDir)) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  });

  describe('basic functionality', () => {
    test('should apply a single edit successfully', async () => {
      const content = 'Hello world\nThis is a test\nGoodbye world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          {
            search: 'This is a test',
            replace: 'This is modified',
            description: 'Update test line'
          }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('Hello world\nThis is modified\nGoodbye world');
      expect(result.changes).toHaveLength(1);
      expect(result.changes![0].edit.description).toBe('Update test line');
      
      // Verify file was actually modified
      const fileContent = fs.readFileSync(testFilePath, 'utf-8');
      expect(fileContent).toBe('Hello world\nThis is modified\nGoodbye world');
    });

    test('should apply multiple edits atomically', async () => {
      const content = 'Line 1\nLine 2\nLine 3\nLine 4';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Line 1', replace: 'First' },
          { search: 'Line 3', replace: 'Third' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('First\nLine 2\nThird\nLine 4');
      expect(result.changes).toHaveLength(2);
    });

    test('should apply edits in correct order regardless of position', async () => {
      const content = 'AAA BBB CCC';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'CCC', replace: 'Z' },
          { search: 'AAA', replace: 'X' },
          { search: 'BBB', replace: 'Y' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('X Y Z');
    });
  });

  describe('dry run mode', () => {
    test('should preview changes without modifying file', async () => {
      const content = 'Original content';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Original', replace: 'Modified' }
        ],
        dryRun: true
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('Modified content');
      
      // Verify file was NOT modified
      const fileContent = fs.readFileSync(testFilePath, 'utf-8');
      expect(fileContent).toBe('Original content');
    });
  });

  describe('error handling - pattern not found', () => {
    test('should fail when search pattern does not exist', async () => {
      const content = 'Hello world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'nonexistent', replace: 'foo' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('Search pattern not found');
      expect(result.error).toContain('nonexistent');
    });

    test('should provide helpful context for similar patterns', async () => {
      const content = 'Hello World';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'hello world', replace: 'foo' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('Search pattern not found');
      expect(result.error).toContain('similar text');
    });

    test('should detect whitespace differences', async () => {
      const content = 'Hello  world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Hello world', replace: 'foo' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('Search pattern not found');
    });
  });

  describe('error handling - ambiguous patterns', () => {
    test('should fail when pattern appears multiple times', async () => {
      const content = 'foo bar\nfoo baz\nfoo qux';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'foo', replace: 'replaced' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('appears');
      expect(result.error).toContain('times');
      expect(result.error).toContain('must be unique');
    });

    test('should provide context for multiple occurrences', async () => {
      const content = 'Line 1: test\nLine 2: test\nLine 3: test';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'test', replace: 'modified' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('3 times');
    });
  });

  describe('error handling - overlapping edits', () => {
    test('should detect overlapping edit operations', async () => {
      const content = 'Hello world test';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Hello world', replace: 'Hi' },
          { search: 'world test', replace: 'earth testing' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('overlap');
    });
  });

  describe('edge cases', () => {
    test('should handle empty file', async () => {
      fs.writeFileSync(testFilePath, '');

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'anything', replace: 'something' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('empty file');
    });

    test('should handle file not found', async () => {
      const params: EditFileParams = {
        path: '/nonexistent/file.txt',
        edits: [
          { search: 'foo', replace: 'bar' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      expect(result.error).toContain('File not found');
    });

    test('should handle empty search pattern', async () => {
      const content = 'Hello world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: '', replace: 'foo' }
        ]
      };

      const result = await editFile(params);

      // Empty pattern would match at every position
      expect(result.success).toBe(false);
    });

    test('should handle newlines in search/replace', async () => {
      const content = 'Line 1\nLine 2\nLine 3';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Line 1\nLine 2', replace: 'Single line' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('Single line\nLine 3');
    });

    test('should handle special characters', async () => {
      const content = 'function() { return true; }';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'function() { return true; }', replace: 'const fn = () => true;' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('const fn = () => true;');
    });

    test('should handle replacing with empty string', async () => {
      const content = 'Hello world test';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: ' world', replace: '' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toBe('Hello test');
    });
  });

  describe('atomicity guarantees', () => {
    test('should not modify file if any edit fails', async () => {
      const content = 'Line 1\nLine 2\nLine 3';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Line 1', replace: 'First' },
          { search: 'nonexistent', replace: 'Second' },
          { search: 'Line 3', replace: 'Third' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(false);
      
      // Verify file was NOT modified
      const fileContent = fs.readFileSync(testFilePath, 'utf-8');
      expect(fileContent).toBe('Line 1\nLine 2\nLine 3');
    });
  });

  describe('diff preview', () => {
    test('should generate readable diff preview', async () => {
      const content = 'Hello world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'Hello', replace: 'Hi', description: 'Shorten greeting' }
        ],
        dryRun: true
      };

      const result = await editFile(params);
      const preview = generateDiffPreview(result);

      expect(preview).toContain('Changes to be applied');
      expect(preview).toContain('Shorten greeting');
      expect(preview).toContain('- Hello');
      expect(preview).toContain('+ Hi');
    });

    test('should show error in preview for failed operations', async () => {
      const content = 'Hello world';
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'nonexistent', replace: 'foo' }
        ],
        dryRun: true
      };

      const result = await editFile(params);
      const preview = generateDiffPreview(result);

      expect(preview).toContain('Search pattern not found');
    });
  });

  describe('real-world scenarios', () => {
    test('should refactor variable names across multiple lines', async () => {
      const content = `function calculate() {
  let userId = 123;
  console.log(userId);
  return userId;
}`;
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: 'let userId = 123;', replace: 'let userID = 123;' },
          { search: 'console.log(userId);', replace: 'console.log(userID);' },
          { search: 'return userId;', replace: 'return userID;' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toContain('let userID = 123;');
      expect(result.modifiedContent).toContain('console.log(userID);');
      expect(result.modifiedContent).toContain('return userID;');
    });

    test('should update imports and function calls together', async () => {
      const content = `import { oldFunction } from './utils';

function doWork() {
  return oldFunction();
}`;
      fs.writeFileSync(testFilePath, content);

      const params: EditFileParams = {
        path: testFilePath,
        edits: [
          { search: "import { oldFunction } from './utils';", replace: "import { newFunction } from './utils';" },
          { search: 'return oldFunction();', replace: 'return newFunction();' }
        ]
      };

      const result = await editFile(params);

      expect(result.success).toBe(true);
      expect(result.modifiedContent).toContain('import { newFunction }');
      expect(result.modifiedContent).toContain('return newFunction();');
    });
  });
});
