export const enum ToolbarView {
  graph = 'graph',
  table = 'table',
}

export interface ToolbarViewSettingsProps {
  view: ToolbarView;
}

export interface ToolbarState {
  measurement: ToolbarViewSettingsProps;
  collection: ToolbarViewSettingsProps;
  meterMeasurement: ToolbarViewSettingsProps;
  meterCollection: ToolbarViewSettingsProps;
  selectionReport: ToolbarViewSettingsProps;
}

export type OnChangeToolbarView = (view: ToolbarView) => void;
