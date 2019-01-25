import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {GlobalSearch} from '../components/search-box/GlobalSearch';
import {RootState} from '../reducers/rootReducer';
import {OnClick} from '../types/Types';
import {clearValidationSearch, validationSearch} from '../usecases/search/searchActions';
import {OnSearch, Query} from '../usecases/search/searchModels';

interface DispatchToProps {
  onChange: OnSearch;
  onClear: OnClick;
}

export type GlobalSearchProps = Query & DispatchToProps;

const mapStateToProps = ({search: {validation: {query}}}: RootState): Query =>
  ({query});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  onClear: clearValidationSearch,
  onChange: validationSearch,
}, dispatch);

export const GlobalSearchContainer =
  connect<Query, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GlobalSearch);
