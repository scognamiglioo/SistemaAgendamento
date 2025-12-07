import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { editFile, EditFileParams } from '../tools/edit-file';

describe('edit-file basic', () => {
  let tempDir: string;
  let testFilePath: string;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'edit-test-'));
    testFilePath = path.join(tempDir, 'test.txt');
  });

  afterEach(() => {
    if (fs.existsSync(tempDir)) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  });

  test('should apply a single edit', async () => {
    fs.writeFileSync(testFilePath, 'Hello world');

    const params: EditFileParams = {
      path: testFilePath,
      edits: [{ search: 'Hello', replace: 'Hi' }]
    };

    const result = await editFile(params);

    expect(result.success).toBe(true);
    expect(result.modifiedContent).toBe('Hi world');
  });

  test('should fail on non-existent pattern', async () => {
    fs.writeFileSync(testFilePath, 'Hello world');

    const params: EditFileParams = {
      path: testFilePath,
      edits: [{ search: 'foo', replace: 'bar' }]
    };

    const result = await editFile(params);

    expect(result.success).toBe(false);
    expect(result.error).toContain('not found');
  });
});
