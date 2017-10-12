import * as React from 'react';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Row} from '../../common/components/layouts/row/Row';
import {Title} from '../../common/components/texts/Title';

export const CollectionOverview = props => (
  <div>
    <Row>
      <Title>{translate('collection')}</Title>
    </Row>
    <Row className="Row-right">
      <PeriodSelection/>
    </Row>
  </div>
);
