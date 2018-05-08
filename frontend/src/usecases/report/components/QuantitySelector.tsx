import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {firstUpperTranslated} from '../../../services/translationService';
import {allQuantities, Quantity, RenderableQuantity} from '../../../state/ui/graph/measurement/measurementModels';

interface QuantitySelectorProps {
  selectedQuantities: Quantity[];
  selectQuantities: (quantities: Quantity[]) => void;
}

export const QuantitySelector = ({selectedQuantities, selectQuantities}: QuantitySelectorProps) => {

  const changeQuantities = (event, index, values) => selectQuantities(values);

  const quantityMenuItem = (quantity: RenderableQuantity) => (
    <MenuItem
      key={quantity}
      checked={selectedQuantities.includes(quantity)}
      value={quantity}
      primaryText={quantity}
    />
  );

  return (
    <div style={{padding: '20px 20px 0px'}}>
      <SelectField
        multiple={true}
        hintText={firstUpperTranslated('select quantities')}
        value={selectedQuantities}
        onChange={changeQuantities}
      >
        {allQuantities.heat.map(quantityMenuItem)}
      </SelectField>
    </div>
  );
};
