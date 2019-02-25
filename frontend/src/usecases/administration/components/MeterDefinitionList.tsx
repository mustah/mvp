import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import NavigationCheck from 'material-ui/svg-icons/navigation/check';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {translate} from '../../../services/translationService';
import {DispatchToProps, StateToProps} from '../containers/MeterDefinitionsContainer';
import {MeterDefinitionActions} from './MeterDefinitionActions';

type Props = StateToProps & DispatchToProps;

export const MeterDefinitionList = ({
  deleteMeterDefinition,
  clearError,
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

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <Column>
        <Row>
          <Link to={routes.adminMeterDefinitionsAdd} className="link" key={'add meter definition'}>
            <ButtonAdd label={translate('add meter definition')}/>
          </Link>
        </Row>
        <Grid
          style={{borderTopWidth: 1}}
          data={toArray(entities)}
          scrollable="none"
        >
          <GridColumn headerClassName="left-most" className="left-most" field="name" title={translate('name')} />
          <GridColumn field="medium.name" title={translate('medium')} />
          <GridColumn field="organisation.name" title={translate('organisation')} />
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
