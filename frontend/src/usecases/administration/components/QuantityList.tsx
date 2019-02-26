import {Grid, GridColumn, GridToolbar} from '@progress/kendo-react-grid';
import * as React from 'react';
import {ButtonLink} from '../../../components/buttons/ButtonLink';
import {InputSelectableCell} from '../../../components/inputs/InputSelectableCell';
import {translate} from '../../../services/translationService';
import {DisplayQuantity, Quantity} from '../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {QuantityActions} from './QuantityActions';
import './QuantityList.scss';

interface OwnProps {
  changedQuantities: any;
  definitionQuantities: DisplayQuantity[];
  allQuantities: Quantity[];
  editable: boolean;
}

type Props = OwnProps;

type state = DisplayQuantity[];

export const QuantityList = ({definitionQuantities, allQuantities, changedQuantities, editable}: Props) => {
  let x = 0;
  definitionQuantities.forEach((q) => q.gridIndex = x++);

  const [localQuantities, setQuantities] = React.useState<state>(definitionQuantities);

  const enterInsert = () => {
    const item = {inEdit: true, gridIndex: definitionQuantities.length};
    update(localQuantities, item, 'edit');
  };

  const saveAction = (rowItem) => update(localQuantities, rowItem.dataItem, 'save');

  const cancelAction = (rowItem) => update(localQuantities, rowItem.dataItem, 'cancel');

  const deleteAction = (rowItem) => update(localQuantities, rowItem.dataItem, 'delete');

  const editQuantity = (rowItem) => update(localQuantities, rowItem.dataItem, 'edit');

  const itemChange = (event) => {
    const value = event.value;
    const quantityName = event.field;
    if (!quantityName) {
      return;
    }

    event.dataItem[quantityName] = value;
    update(localQuantities, event.dataItem, 'change');
  };

  const update = (data, item, mode) => {
    item.inEdit = (mode === 'edit' || mode === 'change');

    const index = item.gridIndex;
    if (index < data.length) {
      data[index] = item;
    } else {
      data.push(item);
    }

    if (mode === 'delete') {
      data.splice(index, 1);
    }

    setQuantities(data);
    changedQuantities(data.slice());
  };

  const actions = (rowItem) =>
    (
      editable ?
        (
          <td>
            <QuantityActions
              confirmDelete={deleteAction}
              editAction={editQuantity}
              cancelAction={cancelAction}
              saveAction={saveAction}
              inEdit={rowItem.dataItem.inEdit}
              dataItem={rowItem}
            />
          </td>
        )
        : null
    );

  const quantitySelectChange = (event) => {
    const value = event.value;
    const field = event.field;
    if (!field) {
      return;
    }
    const quantity = allQuantities.find((q) => q.id === value);
    const item = localQuantities[event.dataItem.dataIndex - 1];
    item[field] = quantity ? quantity.name : '';

    update(localQuantities, item, 'change');
  };

  const quantityCell = ({dataItem: {inEdit, quantityName, id}, ...dataItem}) => {
    const value = allQuantities.find((x) => x.name === quantityName);
    return (
      <InputSelectableCell
        id={'quantity-' + id}
        options={allQuantities}
        onChange={quantitySelectChange}
        dataItem={dataItem}
        value={value}
        inEdit={inEdit}
      />
    );
  };

  const addNewQuantityButton = editable ?
    (
      <ButtonLink
        className="k-button k-primary"
        onClick={enterInsert}
      >{translate('add quantity')}
      </ButtonLink>
    )
    : null;
  return (
    <Grid
      style={{marginTop: 32, marginBottom: 32}}
      data={localQuantities}
      scrollable="none"
      onItemChange={itemChange}
      editField="inEdit"
    >
      <GridToolbar>
        {addNewQuantityButton}
      </GridToolbar>

      <GridColumn
        headerClassName="col"
        className="col"
        field="quantityName"
        title={translate('quantity')}
        cell={quantityCell}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        field="displayUnit"
        title={translate('display unit')}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        field="consumption"
        editor={'boolean'}
        title={translate('consumption')}
      />
      <GridColumn
        headerClassName="col"
        className="col"
        field="precision"
        editor="numeric"
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
