import {LegendPayload} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Identifiable, uuid} from '../../types/Types';

export interface LegendItem {
  id: uuid;
  label: string;
  medium: Medium;
  isRowExpanded?: boolean;
}

export interface ViewOption {
  isAllLinesHidden?: boolean;
}

export type MediumViewOptions = { [medium in Medium]: ViewOption };

export interface Report extends Identifiable {
  meters: LegendItem[];
  mediumViewOptions: MediumViewOptions;
}

export interface ReportState {
  hiddenLines: uuid[];
  resolution: TemporalResolution;
  savedReports: ObjectsById<Report>;
  timePeriod: SelectionInterval;
}

export interface Axes {
  left?: string;
  right?: string;
}

type MeasurementOrigin = 'meter' | 'average';

export interface LineProps {
  id: string;
  dataKey: string;
  key: string;
  name: string;
  city?: string;
  address?: string;
  medium?: string;
  stroke: string;
  strokeWidth?: number;
  yAxisId: string;
  origin: MeasurementOrigin;
}

export interface ProprietaryLegendProps extends LegendPayload {
  color: string;
}

export interface GraphContents {
  axes: Axes;
  data: object[];
  legend: ProprietaryLegendProps[];
  lines: LineProps[];
}

export interface ActiveDataPoint {
  color: any;
  dataKey: uuid;
  fill: any;
  name: uuid;
  payload: {name: number; [key: string]: number};
  stroke: any;
  strokeWidth: number;
  unit: string;
  value: number;
}

export interface SelectedReportPayload {
  items: LegendItem[];
  media: Medium[];
  quantities: Quantity[];
}
