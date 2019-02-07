export const enum ToolbarView {
  graph = 'graph',
  table = 'table',
}

export interface ToolbarViewSettings {
  view: ToolbarView;
}

export interface ToolbarState {
  measurement: ToolbarViewSettings;
}

export type OnChangeToolbarView = (view: ToolbarView) => void;
