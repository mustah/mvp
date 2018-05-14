import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {HasContent} from '../../../components/content/HasContent';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {firstUpperTranslated} from '../../../services/translationService';
import {allQuantities, Quantity, RenderableQuantity} from '../../../state/ui/graph/measurement/measurementModels';

interface QuantitySelectorProps {
  selectedIndicators: Medium[];
  selectedQuantities: Quantity[];
  selectQuantities: (quantities: Quantity[]) => void;
}

const style: React.CSSProperties = {padding: '20px 20px 0px'};

export const QuantityDropdown = ({selectedIndicators, selectedQuantities, selectQuantities}: QuantitySelectorProps) => {
  const quantities: Set<Quantity> = new Set();
  selectedIndicators.forEach((indicator) =>
    indicator in allQuantities && allQuantities[indicator].forEach((q) => quantities.add(q)),
  );

  const changeQuantities = (event, index, values) => selectQuantities(values);

  const quantityMenuItem = (quantity: RenderableQuantity) => (
    <MenuItem
      key={quantity}
      checked={selectedQuantities.includes(quantity)}
      value={quantity}
      primaryText={quantity}
    />
  );
  const options = Array.from(quantities.values()).map(quantityMenuItem);
  if (!options.length && selectedQuantities.length) {
    selectQuantities([]);
  }

  const noMediumSelected: string = firstUpperTranslated('select medium');
  const hint: string = firstUpperTranslated('select quantities');

  return (
    <div style={style}>
      <HasContent fallbackContent={<p>{noMediumSelected}</p>} hasContent={options.length > 0}>
        <SelectField
          multiple={true}
          hintText={hint}
          value={selectedQuantities}
          onChange={changeQuantities}
        >
          {options}
        </SelectField>
      </HasContent>
    </div>
  );
};
