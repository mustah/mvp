import {LegendPayload} from 'recharts';
import {Medium} from '../../components/indicators/indicatorWidgetModels';
import {Status, uuid} from '../../types/Types';

export interface ReportState {
  selectedListItems: uuid[];
}

export interface Axes {
  left?: string;
  right?: string;
}

export interface LineProps {
  dataKey: string;
  key: string;
  name: string;
  stroke: string;
  strokeWidth?: number;
  yAxisId: string;
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

export interface Indicator {
  type: Medium;
  title: string;
  state: Status;
  subtitle: string;
  value: number;
  unit: string;
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

// TODO[!must!] create this in redux later!
export const indicators: Indicator[] = [
  {
    type: Medium.current,
    title: 'El',
    state: Status.info,
    value: 0,
    unit: 'kWh/m2',
    subtitle: '',
  },
  {
    type: Medium.coldWater,
    title: 'Kallvatten',
    state: Status.info,
    value: 0,
    unit: 'l/m2',
    subtitle: '',
  },
  {
    type: Medium.warmWater,
    title: 'Varmvatten',
    state: Status.info,
    value: 0,
    unit: 'l/m2',
    subtitle: '',
  },
  {
    type: Medium.districtHeating,
    title: 'Fjärrvärme',
    state: Status.info,
    value: 1.1,
    unit: 'kWh/m2',
    subtitle: '(-2)',
  },
  {
    type: Medium.gas,
    title: 'Gas',
    state: Status.info,
    value: 1.1,
    unit: 'kWh/m2',
    subtitle: '(-2)',
  },
  {
    type: Medium.temperatureInside,
    title: 'Temp Inomhus',
    state: Status.info,
    value: 0,
    unit: '°C',
    subtitle: '',
  },
  {
    type: Medium.temperatureOutside,
    title: 'Temp Utomhus',
    state: Status.info,
    value: 0,
    unit: '°C',
    subtitle: '',
  },
];
