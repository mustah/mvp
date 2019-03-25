import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {translate} from '../../../services/translationService';
import {MeterTabsContainer} from '../../meter/containers/MeterTabsContainer';
import {SearchResultProps} from '../containers/SearchResultContainer';

export const SearchResult = ({
  location: {pathname},
  queryInState,
  validationSearch,
}: SearchResultProps) => {
  const query = pathname.split('/').pop() as string;
  if (query !== queryInState) {
    validationSearch(query);
  }
  return (
    <PageLayout>
      <RowSpaceBetween>
        <MainTitle>
          {translate('search result: {{query}}', {query: decodeURIComponent(query)})}
        </MainTitle>
        <SummaryContainer/>
      </RowSpaceBetween>

      <Paper style={mainContentPaperStyle}>
        <MeterTabsContainer/>
      </Paper>
    </PageLayout>
  );
};
