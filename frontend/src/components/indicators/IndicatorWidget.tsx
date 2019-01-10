import * as React from 'react';
import {colors} from '../../app/themes';
import {thresholdClassName} from '../../helpers/thresholds';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Children} from '../../types/Types';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {WidgetModel} from './indicatorWidgetModels';
import {IconCollection} from '../icons/IconCollection';
import classNames = require('classnames');

interface Props {
  widget: WidgetModel;
  children?: Children;
  className?: string;
  title: string;
}

interface EmptyStateProps {
  className?: string;
  title: string;
}

const NoExpectedMeasurementsWidget =
  ({className, title}: EmptyStateProps) => (
    <Column className={classNames('Indicator-wrapper NoExpectedMeasurementsWidget', className)}>
      <ColumnCenter className={classNames('Indicator', 'info')}>
        <Row className="Indicator-name Row-center">
          <Normal>{title}</Normal>
        </Row>
        <Row className="Row-center Row-bottom">
          {firstUpperTranslated('no measurements expected for the selected period')}
        </Row>
        <Row className="Indicator-subtitle Row-center">
          <IconCollection className="Indicator-icon" color={colors.blue}/>
        </Row>
      </ColumnCenter>
    </Column>
  );

export const IndicatorWidget =
  ({className, title, widget: {total, pending}}: Props) => {

    if (pending === 0 && total === 0) {
      return <NoExpectedMeasurementsWidget title={title} className={className} />;
    }
    const collectionPercent = total ? ((1 - (pending / total)) * 100) : 0;
    const value = collectionPercent.toFixed(1);
    const statusCss = thresholdClassName(collectionPercent);

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
            <IconCollection className="Indicator-icon" color={colors.white}/>
            <Column className="Indicator-details">
              <Normal>{translate('{{pending}} of {{count}}', {pending, count: total})}</Normal>
              <Normal>{translate('measurement missing', {count: total})}</Normal>
            </Column>
          </Row>
        </ColumnCenter>
      </Column>
    );
  };
