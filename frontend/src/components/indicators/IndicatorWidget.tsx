import * as React from 'react';
import {colors} from '../../app/themes';
import {thresholdClassName} from '../../helpers/thresholds';
import {firstUpperTranslated} from '../../services/translationService';
import {Children} from '../../types/Types';
import {Column, ColumnCenter} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal, Xlarge} from '../texts/Texts';
import './IndicatorWidget.scss';
import {WidgetModel} from './indicatorWidgetModels';
import {IconGateway} from '../icons/IconGateway';
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
          <IconGateway className="Indicator-icon" color={colors.blue}/>
        </Row>
      </ColumnCenter>
    </Column>
  );

export const IndicatorWidget =
  ({className, title, widget: {collectionPercentage}}: Props) => {

    if (isNaN(collectionPercentage)) {
      return <NoExpectedMeasurementsWidget title={title} className={className}/>;
    }
    const value = collectionPercentage.toFixed(collectionPercentage === 100.0 ? 0 : 1);
    const statusCss = thresholdClassName(collectionPercentage);

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
        </ColumnCenter>
      </Column>
    );
  };
