import {State} from '../../../types/Types';
import {Indicator, IndicatorType} from '../../common/components/indicators/models/IndicatorModels';

export interface ReportState {
  title: string;
  records: ReportState[];
  error?: string;
  isFetching: boolean;
}

// TODO[!must!] create this in redux later!
export const indicators: Indicator[] = [
  {
    type: IndicatorType.current,
    title: 'El',
    state: State.ok,
    value: '123',
    unit: 'kWh/m2',
    subtitle: '(+5)',
  },
  {
    type: IndicatorType.coldWater,
    title: 'Kallvatten',
    state: State.warning,
    value: '53',
    unit: 'l/m2',
    subtitle: '(+6)',
  },
  {
    type: IndicatorType.warmWater,
    title: 'Varmvatten',
    state: State.warning,
    value: '13',
    unit: 'l/m2',
    subtitle: '(-2)',
  },
  {
    type: IndicatorType.districtHeating,
    title: 'Fjärrvärme',
    state: State.ok,
    value: '1.1',
    unit: 'kWh/m2',
    subtitle: '(-2)',
  },
  {
    type: IndicatorType.temperatureInside,
    title: 'Temp Inomhus',
    state: State.ok,
    value: '22.4',
    unit: '°C',
    subtitle: '(+0.2)',
  },
  {
    type: IndicatorType.temperatureOutside,
    title: 'Temp Utomhus',
    state: State.info,
    value: '13',
    unit: '°C',
    subtitle: '(+2)',
  },
];