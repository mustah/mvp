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
import {clearValidationSearch, validationSearch} from '../../search/searchActions';
import {OnSearch, Query} from '../../search/searchModels';
import {ValidationTabsContainer} from '../containers/ValidationTabsContainer';

interface DispatchToProps {
  search: OnSearch;
  clearSearch: OnClick;
}

type Props = Query & DispatchToProps;

const Validation = ({clearSearch, search, query}: Props) => (
  <MvpPageContainer>
    <Row className="space-between">
      <RowCenter>
        <MainTitle subtitle={translate('meter', {count: 2})}>
          {translate('validation')}
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
      <ValidationTabsContainer/>
    </Paper>
  </MvpPageContainer>
);

const mapStateToProps = ({search: {validation: {query}}}: RootState): Query =>
  ({query});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearSearch: clearValidationSearch,
  search: validationSearch,
}, dispatch);

export const ValidationContainer =
  connect<Query, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Validation);
