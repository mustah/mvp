import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {mainContentPaperStyle} from '../../../app/themes';
import {RowSpaceBetween} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {CallbackWith} from '../../../types/Types';
import {MeterTabsContainer} from '../../meter/containers/MeterTabsContainer';
import {validationSearch} from '../searchActions';

const SearchResult = ({
  location: {pathname},
  queryInState,
  validationSearch,
}: Props) => {
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

interface StateToProps {
  queryInState?: string;
}

interface DispatchToProps {
  validationSearch: CallbackWith<string>;
}

type OwnProps = RouteComponentProps<{searchQuery?: string}>;

type Props = OwnProps & StateToProps & DispatchToProps;

const mapStateToProps = ({
  search: {validation: {query}}
}: RootState): StateToProps => ({
  queryInState: query,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  validationSearch,
}, dispatch);

export const SearchResultContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps, mapDispatchToProps
)(SearchResult);
