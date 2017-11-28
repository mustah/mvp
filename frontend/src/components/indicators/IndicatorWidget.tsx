import * as React from 'react';
import {colors} from '../../app/themes';
import {translate} from '../../services/translationService';
import {Children} from '../../types/Types';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {WidgetModel} from './models/widgetModels';
import {renderIndicatorIcon} from './SelectableIndicatorWidget';
import classNames = require('classnames');

interface Props {
  widget: WidgetModel;
  children?: Children;
  className?: string;
}

export const IndicatorWidget = (props: Props) => {
  const {widget: {total, status, pending, type}} = props;

  const value = ((1 - (pending / total)) * 100).toFixed(1);
  const pendingPercentage = ((pending / total) * 100).toFixed(1);

  return (
    <Column className={classNames('Indicator-wrapper', props.className)}>
      <ColumnCenter className={classNames('Indicator', status)}>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{value}</Xlarge>
          <Normal className="Indicator-unit">%</Normal>
        </Row>
        <Row className="Indicator-subtitle Row-center">
          {renderIndicatorIcon(type, {color: colors.white})}
          <Column>
            <Normal>{pending} / {pendingPercentage}%</Normal>
            <Normal>{translate('of {{count}} metering point', {count: total})}</Normal>
          </Column>
        </Row>
      </ColumnCenter>
    </Column>
  );
};
