import {Overwrite} from 'utility-types';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {ErrorResponse, Identifiable, UnixTimestamp, uuid} from '../../../../types/Types';
import {LegendItem, LegendType, ResolutionAware} from '../../../report/reportModels';
import {SelectionInterval} from '../../../user-selection/userSelectionModels';
import {ToolbarView} from '../../toolbar/toolbarModels';

export interface Measurement extends Identifiable {
  created: UnixTimestamp;
  value?: number;
  quantity: Quantity;
  unit: string;
}

export interface MeasurementState {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error: Maybe<ErrorResponse>;
  measurementResponse: MeasurementResponse;
  isExportingToExcel: boolean;
}

export type MeasurementsByQuantity = Partial<{ [key in Quantity]: Measurement }>;

export interface MeasurementParameters extends ResolutionAware {
  legendItems: LegendItem[];
  reportDateRange: SelectionInterval;
  shouldComparePeriod: boolean;
  shouldShowAverage: boolean;
  view: ToolbarView;
  displayMode?: QuantityDisplayMode;
}

export type FetchMeasurements = (measurementParameters: MeasurementParameters) => void;

export interface Reading {
  id: UnixTimestamp;
  measurements: MeasurementsByQuantity;
}

export interface ExistingReadings {
  [key: number]: Reading;
}

export type PossibleReading = Overwrite<Reading, {measurements?: MeasurementsByQuantity}>;

export interface Readings {
  [key: number]: PossibleReading;
}

export interface MeasurementValue {
  when: number;
  value?: number;
}

export interface TooltipMeta {
  id: uuid;
  quantity: Quantity;
}

export interface MeasurementResponsePart {
  id: string;
  label: string;
  medium?: string;
  meterId?: string;
  name?: string;
  quantity: Quantity;
  unit: string;
  values: MeasurementValue[];
}

export type MeasurementsApiResponse = MeasurementResponsePart[];

export interface MeasurementResponse {
  average: MeasurementsApiResponse;
  compare: MeasurementsApiResponse;
  measurements: MeasurementsApiResponse;
}

export interface MeasurementRequestModel {
  label?: string;
  logicalMeterId: uuid[];
  quantity: string[];
  reportAfter: string;
  reportBefore: string;
  resolution: TemporalResolution;
}

export interface QuantityAttributes {
  unit: string;
  displayMode: QuantityDisplayMode;
}

export enum Quantity {
  volume = 'Volume',
  flow = 'Flow',
  energy = 'Energy',
  power = 'Power',
  forwardTemperature = 'Forward temperature',
  returnTemperature = 'Return temperature',
  differenceTemperature = 'Difference temperature',
  temperature = 'Temperature',
  relativeHumidity = 'Relative humidity',
  energyReturn = 'Energy return',
  energyReactive = 'Reactive energy',
  externalTemperature = 'External temperature',
}

export const quantities: Quantity[] = Object.keys(Quantity).map(it => Quantity[it]);

export enum QuantityDisplayMode {
  readout = 'readout',
  consumption = 'consumption'
}

export const unitPerHour = (quantity: string | undefined, unit: string | undefined): string | undefined => {
  if (!quantity || !unit) {
    return unit;
  }
  return quantityAttributes[quantity as Quantity].displayMode === QuantityDisplayMode.consumption
    ? `${unit}/h`
    : unit;
};

export const quantityAttributes: { [q in Quantity]: QuantityAttributes } = {
  [Quantity.energy]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.energyReturn]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.energyReactive]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.externalTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.readout},
  [Quantity.volume]: {unit: 'm³', displayMode: QuantityDisplayMode.consumption},
  [Quantity.power]: {unit: 'W', displayMode: QuantityDisplayMode.readout},
  [Quantity.flow]: {unit: 'm³/h', displayMode: QuantityDisplayMode.readout},
  [Quantity.forwardTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.readout},
  [Quantity.returnTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.readout},
  [Quantity.temperature]: {unit: '°C', displayMode: QuantityDisplayMode.readout},
  [Quantity.relativeHumidity]: {unit: '%', displayMode: QuantityDisplayMode.readout},
  [Quantity.differenceTemperature]: {unit: 'K', displayMode: QuantityDisplayMode.readout},
};

export const getDisplayModeText = (quantity: Quantity | string | undefined): string => {
  const quantityAttribute = quantityAttributes[quantity as Quantity];
  return quantityAttribute && quantityAttribute.displayMode === QuantityDisplayMode.consumption
    ? 'consumption'
    : 'meter value';
};

export enum Medium {
  electricity = 'electricity',
  districtHeating = 'districtHeating',
  gas = 'gas',
  hotWater = 'hotWater',
  roomSensor = 'roomSensor',
  water = 'water',
  unknown = 'unknown',
  districtCooling = 'districtCooling',
}

const mediumTexts: { [medium in Medium]: string } = {
  [Medium.districtHeating]: 'District heating',
  [Medium.gas]: 'Gas',
  [Medium.water]: 'Water',
  [Medium.hotWater]: 'Hot water',
  [Medium.electricity]: 'Electricity',
  [Medium.roomSensor]: 'Room sensor',
  [Medium.unknown]: 'Unknown',
  [Medium.districtCooling]: 'District cooling',
};

const mediumTypes: {[name: string]: Medium} = Object.keys(mediumTexts)
  .reduce((acc, m) => ({...acc, [mediumTexts[m]]: m}), {});

export const getMediumType = (key: string): Medium => mediumTypes[key] || Medium.unknown;

export const getMediumText = (medium: Medium): string => mediumTexts[medium];

export const getGroupHeaderTitle = (type: LegendType): string => {
  const mediumText = getMediumText(type as Medium) || 'average';
  return firstUpperTranslated(mediumText.toLowerCase());
};

export const allQuantitiesMap: { [p in LegendType]: Quantity[] } = {
  [Medium.districtHeating]: [
    Quantity.energy,
    Quantity.volume,
    Quantity.flow,
    Quantity.power,
    Quantity.forwardTemperature,
    Quantity.returnTemperature,
    Quantity.differenceTemperature,
  ],
  [Medium.districtCooling]: [
    Quantity.energy,
    Quantity.volume,
    Quantity.flow,
    Quantity.power,
    Quantity.forwardTemperature,
    Quantity.returnTemperature,
    Quantity.differenceTemperature,
  ],
  [Medium.gas]: [
    Quantity.volume,
  ],
  [Medium.water]: [
    Quantity.volume,
  ],
  [Medium.hotWater]: [
    Quantity.volume,
  ],
  [Medium.electricity]: [
    Quantity.energy,
    Quantity.energyReactive,
    Quantity.energyReturn,
    Quantity.power,
  ],
  [Medium.roomSensor]: [
    Quantity.externalTemperature,
    Quantity.relativeHumidity,
  ],
  [Medium.unknown]: [],
  aggregate: [...quantities],
};

const quantitiesToExclude = [
  Quantity.energyReactive,
  Quantity.energyReturn,
];

export const availableQuantities = (quantity: Quantity) => quantitiesToExclude.indexOf(quantity) === -1;

export const weightedQuantity: { [p in Quantity]: number } = {
  [Quantity.energy]: 200,
  [Quantity.volume]: 190,
  [Quantity.flow]: 180,
  [Quantity.power]: 170,
  [Quantity.forwardTemperature]: 160,
  [Quantity.returnTemperature]: 150,
  [Quantity.differenceTemperature]: 140,
  [Quantity.temperature]: 130,
  [Quantity.externalTemperature]: 120,
  [Quantity.relativeHumidity]: 110,
  [Quantity.energyReactive]: 50,
  [Quantity.energyReturn]: 40,
};

export const quantityComparator = (a: Quantity, b: Quantity): number =>
  weightedQuantity[a] < weightedQuantity[b] ? 1 : -1;
