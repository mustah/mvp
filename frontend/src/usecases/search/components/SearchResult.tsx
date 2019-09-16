import Paper from 'material-ui/Paper';
import * as React from 'react';
import {mainContentPaperStyle} from '../../../app/themes';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
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
      </RowSpaceBetween>

      <Paper style={mainContentPaperStyle}>
        <MeterTabsContainer/>
      </Paper>
    </PageLayout>
  );
};
