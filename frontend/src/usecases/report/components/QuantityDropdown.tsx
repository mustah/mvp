import {MenuItem} from 'material-ui';
import * as React from 'react';
import {menuItemStyle} from '../../../app/themes';
import {
  MultiSelectDropdownMenu,
  MultiSelectDropdownMenuProps
} from '../../../components/dropdown-selector/DropdownMenu';
import {withContent} from '../../../components/hoc/withContent';
import {allQuantities, Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {canToggleMedia} from '../../../state/ui/indicator/indicatorActions';
import {HasContent} from '../../../types/Types';

interface QuantitySelectorProps {
  selectedQuantities: Quantity[];
  selectedIndicators: Medium[];
  onSelectQuantities: (quantities: Quantity[]) => void;
}

const quantityMenuItemStyle: React.CSSProperties = {...menuItemStyle, overflow: 'hidden'};

const MultiSelectDropdownMenuWrapper =
  withContent<MultiSelectDropdownMenuProps & HasContent>(MultiSelectDropdownMenu);

const renderQuantityMenuItem = (selectedQuantities: Quantity[]) =>
  (quantity: Quantity) => (
    <MenuItem
      className="DropdownMenu-MenuItem QuantityMenuItem"
      checked={selectedQuantities.includes(quantity)}
      disabled={!selectedQuantities.includes(quantity) && !canToggleMedia(selectedQuantities, quantity)}
      key={quantity}
      primaryText={quantity}
      style={quantityMenuItemStyle}
      value={quantity}
    />
  );

export const QuantityDropdown =
  ({selectedIndicators, selectedQuantities, onSelectQuantities}: QuantitySelectorProps) => {
    const quantities: Set<Quantity> = new Set();
    selectedIndicators.forEach((indicator) =>
      indicator in allQuantities && allQuantities[indicator].forEach((q) => quantities.add(q)),
    );

    const changeQuantities = (event, index, values) => onSelectQuantities(values);
    const options = Array.from(quantities.values()).map(renderQuantityMenuItem(selectedQuantities));

    if (!options.length && selectedQuantities.length) {
      onSelectQuantities([]);
    }

    const wrappedProps: HasContent & MultiSelectDropdownMenuProps = {
      changeQuantities,
      selectedQuantities,
      hasContent: options.length > 0,
    };

    return (
      <MultiSelectDropdownMenuWrapper {...wrappedProps}>
        {options}
      </MultiSelectDropdownMenuWrapper>
    );
  };
