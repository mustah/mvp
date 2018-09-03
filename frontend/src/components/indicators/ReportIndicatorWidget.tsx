import * as classNames from 'classnames';
import * as React from 'react';
import 'ReportIndicatorWidget.scss';
import {colors} from '../../app/themes';
import {OnClick} from '../../types/Types';
import {Indicator} from '../../usecases/report/reportModels';
import {IconWater} from '../icons/IconWater';
import {IconCollection} from '../icons/IconCollection';
import {IconCurrent} from '../icons/IconCurrent';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {IconGas} from '../icons/IconGas';
import {IconTemperature} from '../icons/IconTemperature';
import {IconValidation} from '../icons/IconValidation';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import {Small} from '../texts/Texts';
import {Medium, OnSelectIndicator} from './indicatorWidgetModels';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface IndicatorComponentType {
  [type: string]: React.ComponentType<SvgIconProps>;
}

const indicatorIconFor: IndicatorComponentType = {
  [Medium.collection]: IconCollection,
  [Medium.measurementQuality]: IconValidation,
  [Medium.electricity]: IconCurrent,
  [Medium.water]: IconWater,
  [Medium.hotWater]: IconWater,
  [Medium.districtHeating]: IconDistrictHeating,
  [Medium.gas]: IconGas,
  [Medium.temperatureInside]: IconTemperature,
  [Medium.temperatureOutside]: IconTemperature,
};

export const iconComponentFor =
  (type: Medium): React.ComponentType<SvgIconProps> => indicatorIconFor[type];

const style: React.CSSProperties = {
  width: '24px',
  height: '24px',
};

interface IndicatorProps {
  indicator: Indicator;
  onClick: OnSelectIndicator;
  isSelected?: boolean;
}

export const ReportIndicatorWidget =
  ({onClick, indicator: {state, title, type}, isSelected}: IndicatorProps) => {

    const selectWidget: OnClick = () => onClick(type);

    const IndicatorIcon = iconComponentFor(type);

    return (
      <Column className="ReportIndicatorWidget" onClick={selectWidget}>
        <ColumnCenter className={classNames('Indicator', state)}>
          <RowCenter className="Indicator-icon-wrapper">
            <RowCenter className="Indicator-icon-bg">
              <IndicatorIcon style={style} className="Indicator-icon" color={colors.white}/>
            </RowCenter>
          </RowCenter>
          <RowCenter className="Indicator-name">
            <Small>{title}</Small>
          </RowCenter>
        </ColumnCenter>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    );
  };
