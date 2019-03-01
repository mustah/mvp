export const enum ToolbarView {
  graph = 'graph',
  table = 'table',
}

export interface ToolbarViewSettings {
  view: ToolbarView;
}

export interface ToolbarState {
  measurement: ToolbarViewSettings;
  collection: ToolbarViewSettings;
}

export type OnChangeToolbarView = (view: ToolbarView) => void;
