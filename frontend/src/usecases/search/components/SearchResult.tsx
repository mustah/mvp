import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {PageLayout} from '../../../containers/PageLayout';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';

export const SearchResult = () => (
  <PageLayout>
    <RowSpaceBetween>
      <MainTitle>{translate('search result')}</MainTitle>
      <SummaryContainer/>
    </RowSpaceBetween>

    <Paper style={mainContentPaperStyle}>
      <MeterListContainer componentId="searchResultList"/>
    </Paper>
  </PageLayout>
);
