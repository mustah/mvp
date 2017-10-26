import * as classNames from 'classnames';
import * as React from 'react';
import {IconCollection} from '../icons/IconCollection';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold, Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {Indicator, IndicatorType} from './models/IndicatorModels';
import SvgIconProps = __MaterialUI.SvgIconProps;
import {IconReport} from '../icons/IconReport';

interface IndicatorIcon {
  [type: string]: React.ReactElement<SvgIconProps>;
}

const iconFor: IndicatorIcon = {
  [IndicatorType.collection]: <IconCollection className="Indicator-icon"/>,
  [IndicatorType.measurementQuality]: <IconReport className="Indicator-icon"/>,
};

interface IndicatorProps {
  indicator: Indicator;
  select: (type: IndicatorType) => void;
  isSelected?: boolean;
}

export const IndicatorWidget = (props: IndicatorProps) => {
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
            {iconFor[indicator.type]}
            <Bold>{subtitle}</Bold>
          </Row>
        </Column>

        <div className={classNames('Indicator-separator', {isSelected}, state)}/>
      </Column>
    </div>
  );
};
