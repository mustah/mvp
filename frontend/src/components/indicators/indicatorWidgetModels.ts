import {Medium} from '../../state/ui/graph/measurement/measurementModels';

export interface WidgetModel {
  total: number;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;
