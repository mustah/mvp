import * as React from 'react';
import {compose} from 'recompose';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {EntityTypes, OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {
  ClearErrorPaginated, Clickable, ComponentId,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated, HasContent,
  OnClickWithId,
  uuid
} from '../../types/Types';
import {MeterList} from '../../usecases/validation/components/MeterList';
import {SelectionResultActionsDropdown} from '../actions-dropdown/SelectionResultActionsDropdown';
import {componentOrNothing} from '../hoc/hocs';
import {withContent} from '../hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../hoc/withEmptyContent';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {Column} from '../layouts/column/Column';
import {Loader} from '../loading/Loader';

interface SelectionPage {
  isSelectionPage: boolean;
}

export interface MeterListStateToProps extends SelectionPage {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  isSuperAdmin: boolean;
}

export interface MeterListDispatchToProps {
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
  syncAllMeters: (ids: uuid[]) => void;
  fetchMeters: FetchPaginated;
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
}

type Props = MeterListStateToProps & MeterListDispatchToProps & ComponentId;

const MeterListWrapper = withEmptyContent<Props & WithEmptyContentProps>(MeterList);

const selectionPageEnhancer = componentOrNothing<SelectionPage>(({isSelectionPage}: SelectionPage) => isSelectionPage);

const enhance = compose<Clickable, Clickable & HasContent & SelectionPage>(
  selectionPageEnhancer,
  connectedSuperAdminOnly,
  withContent,
);

const SuperAdminSelectionResultActionsDropdown = enhance(SelectionResultActionsDropdown);

export class MeterListContent extends React.Component<Props> {

  componentDidMount() {
    const {fetchMeters, parameters, pagination: {page}} = this.props;
    fetchMeters(page, parameters);
  }

  componentWillReceiveProps({fetchMeters, parameters, pagination: {page}}: Props) {
    fetchMeters(page, parameters);
  }

  render() {
    const {result, isFetching, isSelectionPage, error} = this.props;
    const {children, ...otherProps} = this.props;
    const hasContent = result.length > 0;
    const wrapperProps: Props & WithEmptyContentProps = {
      ...otherProps,
      noContentText: firstUpperTranslated('no meters'),
      hasContent,
    };

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <Column>
          <SuperAdminSelectionResultActionsDropdown
            onClick={this.syncAllMeters}
            hasContent={hasContent}
            isSelectionPage={isSelectionPage}
          />
          <MeterListWrapper {...wrapperProps}/>
        </Column>
      </Loader>
    );
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});

  syncAllMeters = () => this.props.syncAllMeters(this.props.result);
}
