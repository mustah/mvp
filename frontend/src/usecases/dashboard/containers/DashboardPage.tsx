import * as React from 'react';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {translate} from '../../../services/translationService';
import {DashboardContainer} from './DashboardContainer';

export const DashboardPage = () => (
  <PageLayout>
    <Row className="space-between">
      <MainTitle>{translate('dashboard')}</MainTitle>
    </Row>

    <DashboardContainer/>
  </PageLayout>
);
