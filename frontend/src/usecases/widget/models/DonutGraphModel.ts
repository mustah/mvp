import {WidgetModel} from './WidgetModel';

export interface GraphRecord {
  name: string;
  value: number;
}

export class DonutGraphModel extends WidgetModel {
  records: GraphRecord[];
}
