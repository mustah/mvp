import * as React from 'react';
import {colors} from '../../app/themes';
import {statusClassName} from '../../helpers/thresholds';
import {translate} from '../../services/translationService';
import {Children} from '../../types/Types';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {WidgetModel} from './indicatorWidgetModels';
import {iconComponentFor} from './ReportIndicatorWidget';
import classNames = require('classnames');

interface Props {
  widget: WidgetModel;
  children?: Children;
  className?: string;
  title: string;
}

export const IndicatorWidget =
  ({className, title, widget: {total, pending, type}}: Props) => {
  const collectionPercent = total ? ((1 - (pending / total)) * 100) : 0;
  const value = collectionPercent.toFixed(1);
  const IndicatorIcon = iconComponentFor(type);
  const statusCss = statusClassName(collectionPercent);

  return (
      <Column className={classNames('Indicator-wrapper', className)}>
        <ColumnCenter className={classNames('Indicator', statusCss)}>
          <Row className="Indicator-name Row-center">
            <Normal>{title}</Normal>
          </Row>
          <Row className="Row-center Row-bottom">
            <Xlarge className="Indicator-value">{value}</Xlarge>
            <Normal className="Indicator-unit">%</Normal>
          </Row>
          <Row className="Indicator-subtitle Row-center">
            <IndicatorIcon className="Indicator-icon" color={colors.white}/>
            <Column className="Indicator-details">
              <Normal>{translate('{{pending}} of {{count}}', {pending, count: total})}</Normal>
              <Normal>{translate('measurement', {count: total})}</Normal>
            </Column>
          </Row>
        </ColumnCenter>
      </Column>
    );
  };
