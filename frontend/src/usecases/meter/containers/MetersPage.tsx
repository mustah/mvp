import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {translate} from '../../../services/translationService';
import {MeterTabsContainer} from './MeterTabsContainer';

export const MetersPage = () => (
  <PageLayout>
    <RowSpaceBetween>
      <MainTitle>{translate('meter', {count: 2})}</MainTitle>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <MeterTabsContainer/>
    </Paper>
  </PageLayout>
);
