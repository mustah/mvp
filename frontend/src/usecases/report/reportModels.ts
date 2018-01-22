import {Status, uuid} from '../../types/Types';
import {IndicatorType} from '../../components/indicators/indicatorWidgetModels';

export interface ReportState {
  selectedListItems: uuid[];
}

interface Axes {
  left?: string;
  right?: string;
}

export interface LineProps {
  stroke: string;
  key: string;
  name: string;
  dataKey: string;
}

export interface GraphContents {
  axes: Axes;
  lines: LineProps[];
  data: object[];
}

export interface Indicator {
  type: IndicatorType;
  title: string;
  state: Status;
  subtitle: string;
  value: number;
  unit: string; // Unit is what we are measuring the value in, like "kWh", "m^3"
}

// TODO[!must!] create this in redux later!
export const indicators: Indicator[] = [
  {
    type: IndicatorType.current,
    title: 'El',
    state: Status.info,
    value: 0,
    unit: 'kWh/m2',
    subtitle: '',
  },
  {
    type: IndicatorType.coldWater,
    title: 'Kallvatten',
    state: Status.info,
    value: 0,
    unit: 'l/m2',
    subtitle: '',
  },
  {
    type: IndicatorType.warmWater,
    title: 'Varmvatten',
    state: Status.info,
    value: 0,
    unit: 'l/m2',
    subtitle: '',
  },
  {
    type: IndicatorType.districtHeating,
    title: 'Fjärrvärme',
    state: Status.ok,
    value: 1.1,
    unit: 'kWh/m2',
    subtitle: '(-2)',
  },
  {
    type: IndicatorType.temperatureInside,
    title: 'Temp Inomhus',
    state: Status.info,
    value: 0,
    unit: '°C',
    subtitle: '',
  },
  {
    type: IndicatorType.temperatureOutside,
    title: 'Temp Utomhus',
    state: Status.info,
    value: 0,
    unit: '°C',
    subtitle: '',
  },
];
