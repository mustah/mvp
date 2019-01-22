import {Medium} from '../../state/ui/graph/measurement/measurementModels';

export interface WidgetModel {
  collectionPercentage: number;
}

export type OnSelectIndicator = (type: Medium) => void;
