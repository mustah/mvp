import * as React from 'react';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Title';
import {PageContainer} from '../../common/containers/PageContainer';
import ValidationTabsContainer from '../containers/ValidationTabsContainer';

export const Validation = () => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('validation')}</MainTitle>
        <PeriodSelection/>
      </Row>

      <ValidationTabsContainer/>

    </PageContainer>
  );
};
