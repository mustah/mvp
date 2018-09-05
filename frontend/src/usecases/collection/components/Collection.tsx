import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {mainContentPaperStyle} from '../../../app/themes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {SearchBox} from '../../../components/search-box/SearchBox';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {OnClick} from '../../../types/Types';
import {clearCollectionSearch, collectionSearch} from '../../search/searchActions';
import {OnSearch, Query} from '../../search/searchModels';
import {CollectionTabsContainer} from '../containers/CollectionTabsContainer';

interface DispatchToProps {
  search: OnSearch;
  clearSearch: OnClick;
}

type Props = Query & DispatchToProps;

const Collection = ({clearSearch, search, query}: Props) => (
  <MvpPageContainer>
    <Row className="space-between">
      <RowCenter>
        <MainTitle subtitle={translate('gateways')}>
          {translate('collection')}
        </MainTitle>
        <SearchBox
          onChange={search}
          onClear={clearSearch}
          value={query}
          className="SearchBox-list"
        />
      </RowCenter>
      <Row>
        <SummaryContainer/>
        <PeriodContainer/>
      </Row>
    </Row>

    <Paper style={mainContentPaperStyle}>
      <CollectionTabsContainer/>
    </Paper>
  </MvpPageContainer>
);

const mapStateToProps = ({search: {collection: {query}}}: RootState): Query => ({query});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearSearch: clearCollectionSearch,
  search: collectionSearch,
}, dispatch);

export const CollectionContainer =
  connect<Query, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Collection);
