import * as classNames from 'classnames';
import * as React from 'react';
import {Indicator} from '../../usecases/report/reportModels';
import {IconColdWater} from '../icons/IconColdWater';
import {IconCollection} from '../icons/IconCollection';
import {IconCurrent} from '../icons/IconCurrent';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {IconTemperature} from '../icons/IconTemperature';
import {IconValidation} from '../icons/IconValidation';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold, Normal, Xlarge} from '../texts/Texts';
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
  [IndicatorType.temperatureInside]: IconTemperature,
  [IndicatorType.temperatureOutside]: IconTemperature,
};

export const iconComponentFor = (type: IndicatorType): React.ComponentType<SvgIconProps> => indicatorIconFor[type];

interface IndicatorProps {
  indicator: Indicator;
  select: OnSelectIndicator;
  isSelected?: boolean;
}

export const SelectableIndicatorWidget = (props: IndicatorProps) => {
  const {select, indicator, isSelected} = props;
  const {state, title, value, unit, subtitle, type} = indicator;

  const selectWidget = () => select(type);
  const isEnabled = type === IndicatorType.districtHeating;

  const IndicatorIcon = iconComponentFor(type);

  return (
    <div onClick={selectWidget}>
      <Column className="Indicator-wrapper">
        <Column className={classNames('Indicator Column-center', state)}>
          <Row className="Indicator-name Row-center">
            <Bold>{title}</Bold>
          </Row>
          <Row className="Row-center Row-bottom">
            <Xlarge className="Indicator-value">{(isEnabled && value) || '-'}</Xlarge>
            <Normal className="Indicator-unit">{isEnabled && unit}</Normal>
          </Row>
          <Row className="Indicator-subtitle Row-center">
            <IndicatorIcon className="Indicator-icon" color="black"/>
            <Bold>{subtitle}</Bold>
          </Row>
        </Column>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    </div>
  );
};
