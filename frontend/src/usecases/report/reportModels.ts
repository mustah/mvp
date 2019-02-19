import {LegendPayload} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {firstUpperTranslated} from '../../services/translationService';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {Identifiable, uuid} from '../../types/Types';

export interface LegendItem {
  id: uuid;
  label: string;
  medium: Medium;
}

export interface Report extends Identifiable {
  meters: LegendItem[];
}

export interface ReportState {
  isAllLinesHidden: boolean;
  hiddenLines: uuid[];
  resolution: TemporalResolution;
  savedReports: ObjectsById<Report>;
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

interface ReportIndicatorProps {
  enabled?: boolean;
  type: Medium;
  title: string;
  isSelected?: boolean;
}

// TODO[!must!] remove later
export const reportIndicators = (): ReportIndicatorProps[] => ([
  {
    type: Medium.electricity,
    title: firstUpperTranslated('electricity'),
  },
  {
    type: Medium.hotWater,
    title: firstUpperTranslated('hot water'),
  },
  {
    type: Medium.water,
    title: firstUpperTranslated('water'),
  },
  {
    type: Medium.districtHeating,
    title: firstUpperTranslated('district heating'),
  },
  {
    type: Medium.gas,
    title: firstUpperTranslated('gas'),
  },
  {
    type: Medium.roomSensor,
    title: firstUpperTranslated('room sensor'),
  },
]);
