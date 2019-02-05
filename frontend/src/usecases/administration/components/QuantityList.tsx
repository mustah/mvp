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

  const getQuantityByName = (name: string): QuantityMaybeId =>
    localQuantities[localQuantities.findIndex((q) => q.name === name)];

  const enterInsert = () => {
    const quantity = {inEdit: true};
    update(localQuantities, quantity, 'edit');
  };

  const saveAction = (name) => {
    update(localQuantities, getQuantityByName(name), 'save');
  };

  const cancelAction = (name) => {
    update(localQuantities, getQuantityByName(name), 'cancel');
  };

  const deleteAction = (name) => {
    update(localQuantities, getQuantityByName(name), 'delete');
  };

  const editQuantity = (name) => {
    update(localQuantities, getQuantityByName(name), 'edit');
  };

  const itemChange = (event) => {
    const value = event.value;
    const name = event.field;
    if (!name) {
      return;
    }

    event.dataItem[name] = value;
    update(localQuantities, event.dataItem, 'change');
  };

  const update = (data, item, mode) => {
    item.inEdit = (mode === 'edit' || mode === 'change');

    const updated = {...item};
    let index = data.findIndex((p) => p === item || item.name && p.name === item.name);
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

  const actions = ({dataItem: {name, inEdit}}) =>
    (
      <td>
        <QuantityActions
          confirmDelete={deleteAction}
          editAction={editQuantity}
          cancelAction={cancelAction}
          saveAction={saveAction}
          inEdit={inEdit}
          id={name}
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
        field="name"

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
