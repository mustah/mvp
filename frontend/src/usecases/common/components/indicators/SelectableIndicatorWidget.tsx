import * as classNames from 'classnames';
import * as React from 'react';
import {Indicator} from '../../../report/models/reportModels';
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
import {IndicatorType} from './models/widgetModels';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface IndicatorIcon {
  [type: string]: React.ReactElement<SvgIconProps>;
}

const indicatorIconFor: IndicatorIcon = {
  [IndicatorType.collection]: <IconCollection className="Indicator-icon" color="black"/>,
  [IndicatorType.measurementQuality]: <IconValidation className="Indicator-icon" color="black"/>,
  [IndicatorType.current]: <IconCurrent className="Indicator-icon" color="black"/>,
  [IndicatorType.coldWater]: <IconColdWater className="Indicator-icon" color="black"/>,
  [IndicatorType.warmWater]: <IconColdWater className="Indicator-icon" color="black"/>,
  [IndicatorType.districtHeating]: <IconDistrictHeating className="Indicator-icon"/>,
  [IndicatorType.temperatureInside]: <IconTemperature className="Indicator-icon" color="black"/>,
  [IndicatorType.temperatureOutside]: <IconTemperature className="Indicator-icon" color="black"/>,
};

export const renderIndicator = (type: IndicatorType, props?: SvgIconProps): React.ReactElement<SvgIconProps> => {
  return React.cloneElement(indicatorIconFor[type], props);
};

interface IndicatorProps {
  indicator: Indicator;
  select: (type: IndicatorType) => void;
  isSelected?: boolean;
}

export const SelectableIndicatorWidget = (props: IndicatorProps) => {
  const {select, indicator, isSelected} = props;
  const {state, title, value, unit, subtitle} = indicator;

  const selectWidget = () => select(indicator.type);

  return (
    <div onClick={selectWidget}>
      <Column className="Indicator-wrapper">
        <Column className={classNames('Indicator Column-center', state)}>
          <Row className="Indicator-name Row-center">
            <Bold>{title}</Bold>
          </Row>
          <Row className="Row-center Row-bottom">
            <Xlarge className="Indicator-value">{value}</Xlarge>
            <Normal className="Indicator-unit">{unit}</Normal>
          </Row>
          <Row className="Indicator-subtitle Row-center">
            {renderIndicator(indicator.type)}
            <Bold>{subtitle}</Bold>
          </Row>
        </Column>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    </div>
  );
};
