// Tool implementations
export * from './tools/edit-file';
export * from './tools/apply-diff';
export * from './tools/definitions';

// Main entry point for the tools
import { editFile, EditFileParams, EditResult } from './tools/edit-file';
import { applyDiff, ApplyDiffParams, ApplyDiffResult } from './tools/apply-diff';
import { TOOL_DEFINITIONS, getToolDefinition, validateToolParams } from './tools/definitions';

export const tools = {
  editFile,
  applyDiff
};

export const toolDefinitions = TOOL_DEFINITIONS;

export {
  getToolDefinition,
  validateToolParams
};
