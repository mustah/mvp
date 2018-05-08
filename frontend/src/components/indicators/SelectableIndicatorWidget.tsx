import * as classNames from 'classnames';
import * as React from 'react';
import {colors} from '../../app/themes';
import {Indicator} from '../../usecases/report/reportModels';
import {IconColdWater} from '../icons/IconColdWater';
import {IconCollection} from '../icons/IconCollection';
import {IconCurrent} from '../icons/IconCurrent';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {IconTemperature} from '../icons/IconTemperature';
import {IconValidation} from '../icons/IconValidation';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './IndicatorWidget.scss';
import {IndicatorType, OnSelectIndicator} from './indicatorWidgetModels';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface IndicatorComponentType {
  [type: string]: React.ComponentType<SvgIconProps>;
}

const indicatorIconFor: IndicatorComponentType = {
  [IndicatorType.collection]: IconCollection,
  [IndicatorType.measurementQuality]: IconValidation,
  [IndicatorType.current]: IconCurrent,
  [IndicatorType.coldWater]: IconColdWater,
  [IndicatorType.warmWater]: IconColdWater,
  [IndicatorType.districtHeating]: IconDistrictHeating,
  [IndicatorType.gas]: IconDistrictHeating,
  [IndicatorType.temperatureInside]: IconTemperature,
  [IndicatorType.temperatureOutside]: IconTemperature,
};

export const iconComponentFor =
  (type: IndicatorType): React.ComponentType<SvgIconProps> => indicatorIconFor[type];

const style: React.CSSProperties = {
  width: '48px',
  height: '48px',
};

interface IndicatorProps {
  indicator: Indicator;
  onClick: OnSelectIndicator;
  isSelected?: boolean;
}

export const SelectableIndicatorWidget = ({onClick, indicator, isSelected}: IndicatorProps) => {
  const {state, title, type} = indicator;

  const selectWidget = () => onClick(type);

  const IndicatorIcon = iconComponentFor(type);

  return (
    <div onClick={selectWidget}>
      <Column className="Indicator-wrapper">
        <ColumnCenter className={classNames('Indicator', state)}>
          <Row className="Indicator-name Row-center">
            <Bold>{title}</Bold>
          </Row>
          <Row className="Row-center Row-bottom">
            <IndicatorIcon style={style} className="Indicator-icon" color={isSelected ? colors.white : colors.black}/>
          </Row>
        </ColumnCenter>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    </div>
  );
};
