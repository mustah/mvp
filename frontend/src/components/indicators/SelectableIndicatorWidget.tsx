import * as classNames from 'classnames';
import * as React from 'react';
import {colors} from '../../app/themes';
import {OnClick, Status} from '../../types/Types';
import {Indicator} from '../../usecases/report/reportModels';
import {IconColdWater} from '../icons/IconColdWater';
import {IconCollection} from '../icons/IconCollection';
import {IconCurrent} from '../icons/IconCurrent';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {IconGas} from '../icons/IconGas';
import {IconTemperature} from '../icons/IconTemperature';
import {IconValidation} from '../icons/IconValidation';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './IndicatorWidget.scss';
import {Medium, OnSelectIndicator} from './indicatorWidgetModels';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface IndicatorComponentType {
  [type: string]: React.ComponentType<SvgIconProps>;
}

const indicatorIconFor: IndicatorComponentType = {
  [Medium.collection]: IconCollection,
  [Medium.measurementQuality]: IconValidation,
  [Medium.electricity]: IconCurrent,
  [Medium.coldWater]: IconColdWater,
  [Medium.hotWater]: IconColdWater,
  [Medium.districtHeating]: IconDistrictHeating,
  [Medium.gas]: IconGas,
  [Medium.temperatureInside]: IconTemperature,
  [Medium.temperatureOutside]: IconTemperature,
};

export const iconComponentFor =
  (type: Medium): React.ComponentType<SvgIconProps> => indicatorIconFor[type];

const style: React.CSSProperties = {
  width: '48px',
  height: '48px',
};

interface IndicatorProps {
  indicator: Indicator;
  onClick: OnSelectIndicator;
  isSelected?: boolean;
}

export const SelectableIndicatorWidget = ({onClick, indicator: {state, title, type}, isSelected}: IndicatorProps) => {

  const selectWidget: OnClick = () => onClick(type);

  const IndicatorIcon = iconComponentFor(type);

  const foreground: string = isSelected && [Status.ok].includes(state)
    ? colors.white
    : colors.black;

  return (
    <div onClick={selectWidget}>
      <Column className="Indicator-wrapper">
        <ColumnCenter className={classNames('Indicator', state)}>
          <Row className="Indicator-name Row-center">
            <Bold>{title}</Bold>
          </Row>
          <Row className="Row-center Row-bottom">
            <IndicatorIcon style={style} className="Indicator-icon" color={foreground}/>
          </Row>
        </ColumnCenter>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    </div>
  );
};
