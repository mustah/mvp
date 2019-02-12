import * as React from 'react';
import {LegendPayload} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {IconCurrent} from '../../components/icons/IconCurrent';
import {IconDistrictHeating} from '../../components/icons/IconDistrictHeating';
import {IconGas} from '../../components/icons/IconGas';
import {IconTemperature} from '../../components/icons/IconTemperature';
import {IconUnknown} from '../../components/icons/IconUnknown';
import {IconWater} from '../../components/icons/IconWater';
import {firstUpperTranslated} from '../../services/translationService';
import {Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';
import SvgIconProps = __MaterialUI.SvgIconProps;

export interface ReportState {
  selectedListItems: uuid[];
  hiddenLines: uuid[];
  resolution: TemporalResolution;
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
  medium: Medium | Medium[];
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

export interface SelectedReportEntriesPayload {
  ids: uuid[];
  indicatorsToSelect: Medium[];
  quantitiesToSelect: Quantity[];
}

type IndicatorComponentType = { [type in Medium]: React.ComponentType<SvgIconProps> };

const mediumIcons: IndicatorComponentType = {
  [Medium.electricity]: IconCurrent,
  [Medium.water]: IconWater,
  [Medium.hotWater]: IconWater,
  [Medium.districtHeating]: IconDistrictHeating,
  [Medium.gas]: IconGas,
  [Medium.roomSensor]: IconTemperature,
  [Medium.unknown]: IconUnknown,
};

export const mediumIconComponent =
  (type: Medium): React.ComponentType<SvgIconProps> => mediumIcons[type] || IconUnknown;

export interface ReportIndicatorProps {
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
