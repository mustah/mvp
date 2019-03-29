import * as React from 'react';
import {thresholdClassName} from '../../../helpers/thresholds';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {CountableWidgetModel} from '../../../state/domain-models/widget/widgetModels';
import {ClassNamed, WithChildren} from '../../../types/Types';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Bold, Large, Normal, Xlarge} from '../../../components/texts/Texts';
import './IndicatorWidget.scss';
import classNames = require('classnames');

export interface IndicatorWidgetProps extends ClassNamed, WithChildren {
  widget: CountableWidgetModel;
  title: string;
}

interface EmptyStateProps extends ClassNamed {
  title: string;
}

const NoExpectedMeasurementsWidget = ({className, title}: EmptyStateProps) => (
  <Column className={classNames('Indicator-wrapper NoExpectedMeasurementsWidget', className)}>
    <ColumnCenter className={classNames('Indicator', 'info')}>
      <Row className="Indicator-name Row-center">
        <Bold>{title}</Bold>
      </Row>
      <Row className="Row-center Row-bottom">
        {firstUpperTranslated('no measurements expected for the selected period')}
      </Row>
    </ColumnCenter>
  </Column>
);

export const IndicatorWidget = ({className, title, widget: {count}}: IndicatorWidgetProps) => {
  if (isNaN(count)) {
    return <NoExpectedMeasurementsWidget title={title} className={className}/>;
  }
  const value = count.toFixed(count === 100.0 ? 0 : 1);
  return (
    <Column className={classNames('Indicator-wrapper', className)}>
      <ColumnCenter className={classNames('Indicator', thresholdClassName(count))}>
        <Row className="Indicator-name Row-center">
          <Bold>{title}</Bold>
        </Row>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{value}</Xlarge>
          <Normal className="Indicator-unit">%</Normal>
        </Row>
      </ColumnCenter>
    </Column>
  );
};

export const NumMetersIndicatorWidget = ({className, title, widget: {count}}: IndicatorWidgetProps) => {
  if (isNaN(count)) {
    return <NoExpectedMeasurementsWidget title={title} className={className}/>;
  }
  return (
    <Column className={classNames('Indicator-wrapper', className)}>
      <ColumnCenter className={classNames('Indicator', 'count')}>
        <Row className="Indicator-name Row-center">
          <Bold>{title}</Bold>
        </Row>
        <Row className="Row-center Row-bottom">
          <Xlarge className="Indicator-value">{count}</Xlarge>
          <Large style={{paddingBottom: 8}}>{translate('pcs')}</Large>
        </Row>
      </ColumnCenter>
    </Column>
  );
};
