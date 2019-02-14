import {Grid, GridColumn, GridToolbar} from '@progress/kendo-react-grid';
import * as React from 'react';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {translate} from '../../../services/translationService';
import {QuantityMaybeId} from '../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {QuantityActions} from './QuantityActions';
import './QuantityList.scss';

interface OwnProps {
  changedQuantities: any;
  quantities: QuantityMaybeId[];
}

type Props = OwnProps;

type state = QuantityMaybeId[];

export const QuantityList = ({quantities, changedQuantities}: Props) => {
  const [localQuantities, setQuantities] = React.useState<state>(quantities);

  const getQuantityByName = (quanitityName: string): QuantityMaybeId =>
    localQuantities[localQuantities.findIndex((q) => q.quanitityName === quanitityName)];

  const enterInsert = () => {
    const quantity = {inEdit: true};
    update(localQuantities, quantity, 'edit');
  };

  const saveAction = (quanitityName) => {
    update(localQuantities, getQuantityByName(quanitityName), 'save');
  };

  const cancelAction = (quanitityName) => {
    update(localQuantities, getQuantityByName(quanitityName), 'cancel');
  };

  const deleteAction = (quanitityName) => {
    update(localQuantities, getQuantityByName(quanitityName), 'delete');
  };

  const editQuantity = (quanitityName) => {
    update(localQuantities, getQuantityByName(quanitityName), 'edit');
  };

  const itemChange = (event) => {
    const value = event.value;
    const quanitityName = event.field;
    if (!quanitityName) {
      return;
    }

    event.dataItem[quanitityName] = value;
    update(localQuantities, event.dataItem, 'change');
  };

  const update = (data, item, mode) => {
    item.inEdit = (mode === 'edit' || mode === 'change');

    const updated = {...item};
    let index = data.findIndex((p) => p === item || item.quanitityName && p.quanitityName === item.quanitityName);
    if (index >= 0) {
      data[index] = updated;
    } else {
      data.unshift(updated);
      index = 0;
    }

    if (mode === 'delete') {
      data.splice(index, 1);
    }

    setQuantities(data);
    changedQuantities(data.slice());
  };

  const actions = ({dataItem: {quanitityName, inEdit}}) =>
    (
      <td>
        <QuantityActions
          confirmDelete={deleteAction}
          editAction={editQuantity}
          cancelAction={cancelAction}
          saveAction={saveAction}
          inEdit={inEdit}
          id={quanitityName}
        />
      </td>
    );

  return (
    <Grid
      style={{marginTop: 32, marginBottom: 32}}
      data={localQuantities}
      scrollable="none"
      onItemChange={itemChange}
      editField="inEdit"
    >
      <GridToolbar>
        <ButtonLink
          className="k-button k-primary"
          onClick={enterInsert}
        >Add new
        </ButtonLink>
      </GridToolbar>

      <GridColumn
        headerClassName="col"
        className="col"
        field="quanitityName"

        title={translate('quantity')}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        field="displayUnit"
        editable={true}
        title={translate('display unit')}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        field="precision"
        title={translate('precision')}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        cell={actions}
        width={55}
      />
    </Grid>
  );
};
