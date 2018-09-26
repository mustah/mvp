import * as classNames from 'classnames';
import * as React from 'react';
import 'ReportIndicatorWidget.scss';
import {colors} from '../../app/themes';
import {OnClick} from '../../types/Types';
import {IconCurrent} from '../icons/IconCurrent';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {IconGas} from '../icons/IconGas';
import {IconTemperature} from '../icons/IconTemperature';
import {IconUnknown} from '../icons/IconUnknown';
import {IconWater} from '../icons/IconWater';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import {Small} from '../texts/Texts';
import {Medium, OnSelectIndicator} from './indicatorWidgetModels';
import SvgIconProps = __MaterialUI.SvgIconProps;

type IndicatorComponentType = {[type in Medium]: React.ComponentType<SvgIconProps>};
const indicatorIconFor: IndicatorComponentType = {
  [Medium.electricity]: IconCurrent,
  [Medium.water]: IconWater,
  [Medium.hotWater]: IconWater,
  [Medium.districtHeating]: IconDistrictHeating,
  [Medium.gas]: IconGas,
  [Medium.roomSensor]: IconTemperature,
  [Medium.unknown]: IconUnknown,
};

export const iconComponentFor =
  (type: Medium): React.ComponentType<SvgIconProps> => indicatorIconFor[type] || IconUnknown;

const style: React.CSSProperties = {
  width: '24px',
  height: '24px',
};

export interface ReportIndicatorProps {
  enabled?: boolean;
  type: Medium;
  title: string;
  isSelected?: boolean;
}

export interface ClickableReportIndicatorProps extends ReportIndicatorProps {
  onClick: OnSelectIndicator;
}

export const ReportIndicatorWidget =
  ({enabled, onClick, title, type, isSelected}: ClickableReportIndicatorProps) => {

    const selectWidget: OnClick = () => onClick(type);

    const IndicatorIcon = iconComponentFor(type);

    const state = enabled
      ? 'enabled'
      : 'disabled';

    return (
      <Column className="ReportIndicatorWidget" onClick={selectWidget}>
        <ColumnCenter className="Indicator">
          <RowCenter className="Indicator-icon-wrapper">
            <RowCenter className={classNames('Indicator-icon-bg', state)}>
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
