import {LegendPayload} from 'recharts';
import {Medium} from '../../components/indicators/indicatorWidgetModels';
import {ReportIndicatorProps} from '../../components/indicators/ReportIndicatorWidget';
import {firstUpperTranslated} from '../../services/translationService';
import {uuid} from '../../types/Types';

export interface ReportState {
  selectedListItems: uuid[];
}

export interface Axes {
  left?: string;
  right?: string;
}

type MeasurementOrigin = 'meter' | 'average' | 'city';

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

export interface LegendItem {
  facility?: string;
  address?: string;
  city: string;
  medium: string | string[];
  id: uuid;
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

export const hardcodedIndicators = (): ReportIndicatorProps[] => ([
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
    type: Medium.temperatureInside,
    title: firstUpperTranslated('inside temperature'),
  },
]);
