import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import NavigationCheck from 'material-ui/svg-icons/navigation/check';
import * as React from 'react';
import {routes} from '../../../../app/routes';
import {makeGridClassName} from '../../../../app/themes';
import {ButtonAdd} from '../../../../components/buttons/ButtonAdd';
import {useConfirmDialog} from '../../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../../components/dialog/DeleteConfirmDialog';
import {ThemeContext} from '../../../../components/hoc/withThemeProvider';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {Link} from '../../../../components/links/Link';
import {RetryLoader} from '../../../../components/loading/Loader';
import {Maybe} from '../../../../helpers/Maybe';
import {translate} from '../../../../services/translationService';
import {DomainModel} from '../../../../state/domain-models/domainModels';
import {MeterDefinition} from '../../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {ClearError, ErrorResponse, Fetch, OnClickWithId} from '../../../../types/Types';
import {MeterDefinitionActions} from './MeterDefinitionActions';
import './MeterDefinitionList.scss';

export interface StateToProps {
  meterDefinitions: DomainModel<MeterDefinition>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps {
  deleteMeterDefinition: OnClickWithId;
  fetchMeterDefinitions: Fetch;
  clearError: ClearError;
}

type Props = StateToProps & DispatchToProps & ThemeContext;

export const MeterDefinitionList = ({
  cssStyles,
  clearError,
  deleteMeterDefinition,
  error,
  fetchMeterDefinitions,
  isFetching,
  meterDefinitions: {entities},
}: Props) => {
  React.useEffect(() => {
    fetchMeterDefinitions();
  });
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteMeterDefinition);

  const isDefault = ({dataItem: {autoApply}}) => autoApply ? <td><NavigationCheck/></td> : <td/>;
  const actions = ({dataItem: {id}}) => <td><MeterDefinitionActions confirmDelete={openConfirm} id={id}/></td>;

  const renderOrganisation = ({dataItem: {organisation}}) => {
    const organisationName: string = organisation ? organisation.name : 'System';
    return (
      <td className={!organisation ? 'DefaultOrganisation' : ''}>{organisationName}</td>
    );
  };

  const meterDefinitions = toArray(entities);

  return (
    <RetryLoader isFetching={isFetching} clearError={clearError} error={error}>
      <Column>
        <Row>
          <Link to={routes.adminMeterDefinitionsAdd} key={'add meter definition'}>
            <ButtonAdd label={translate('add meter definition')}/>
          </Link>
        </Row>
        <Grid
          className={makeGridClassName(cssStyles)}
          style={{borderTopWidth: 1}}
          data={meterDefinitions}
          scrollable="none"
        >
          <GridColumn headerClassName="left-most" className="left-most" field="name" title={translate('name')}/>
          <GridColumn field="medium.name" title={translate('medium')}/>
          <GridColumn cell={renderOrganisation} title={translate('organisation')}/>
          <GridColumn
            field="autoApply"
            title={translate('default')}
            cell={isDefault}
            width={60}
          />
          <GridColumn cell={actions} width={40}/>
        </Grid>
        <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
      </Column>
    </RetryLoader>
  );
};
