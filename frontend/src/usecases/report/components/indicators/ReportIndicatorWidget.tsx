import {default as classNames} from 'classnames';
import * as React from 'react';
import {colors} from '../../../../app/themes';
import {IconCurrent} from '../../../../components/icons/IconCurrent';
import {IconDistrictHeating} from '../../../../components/icons/IconDistrictHeating';
import {IconGas} from '../../../../components/icons/IconGas';
import {IconTemperature} from '../../../../components/icons/IconTemperature';
import {IconUnknown} from '../../../../components/icons/IconUnknown';
import {IconWater} from '../../../../components/icons/IconWater';
import {Medium, OnSelectIndicator} from '../../../../components/indicators/indicatorWidgetModels';
import {Column, ColumnCenter} from '../../../../components/layouts/column/Column';
import {RowCenter} from '../../../../components/layouts/row/Row';
import {Small} from '../../../../components/texts/Texts';
import {OnClick} from '../../../../types/Types';
import './ReportIndicatorWidget.scss';
import SvgIconProps = __MaterialUI.SvgIconProps;

type IndicatorComponentType = { [type in Medium]: React.ComponentType<SvgIconProps> };

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
