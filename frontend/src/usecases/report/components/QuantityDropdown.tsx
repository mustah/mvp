import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {withEmptyContentComponent} from '../../../components/hoc/withEmptyContent';
import {Bold} from '../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../services/translationService';
import {allQuantities, Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {canToggleMedia} from '../../../state/ui/indicator/indicatorActions';
import {Children, HasContent} from '../../../types/Types';

interface Props {
  selectedQuantities: Quantity[];
  changeQuantities: (event, index, values) => void;
  children?: Children;
}

interface QuantitySelectorProps {
  selectedQuantities: Quantity[];
  selectedIndicators: Medium[];
  onSelectQuantities: (quantities: Quantity[]) => void;
}

const style: React.CSSProperties = {padding: '20px 20px 0px'};

const quantityMenuItem =
  (selectedQuantities: Quantity[]) =>
    (quantity: Quantity) => (
      <MenuItem
        checked={selectedQuantities.includes(quantity)}
        disabled={!selectedQuantities.includes(quantity) && !canToggleMedia(selectedQuantities, quantity)}
        key={quantity}
        primaryText={quantity}
        value={quantity}
      />
    );

const HintText = () =>
  <Bold className="Italic">{firstUpperTranslated('select medium')}</Bold>;

const SelectFieldOptions = ({children, changeQuantities, selectedQuantities}: Props) => (
  <SelectField
    multiple={true}
    hintText={firstUpperTranslated('select quantities')}
    value={selectedQuantities}
    onChange={changeQuantities}
  >
    {children}
  </SelectField>
);

const WrappedSelectFieldOptions = withEmptyContentComponent<Props & HasContent>(
  SelectFieldOptions,
  HintText,
);

export const QuantityDropdown =
  ({selectedIndicators, selectedQuantities, onSelectQuantities}: QuantitySelectorProps) => {
    const quantities: Set<Quantity> = new Set();
    selectedIndicators.forEach((indicator) =>
      indicator in allQuantities && allQuantities[indicator].forEach((q) => quantities.add(q)),
    );

    const changeQuantities = (event, index, values) => onSelectQuantities(values);
    const renderMenuItem = quantityMenuItem(selectedQuantities);
    const options = Array.from(quantities.values()).map(renderMenuItem);

    if (!options.length && selectedQuantities.length) {
      onSelectQuantities([]);
    }

    const wrappedProps: HasContent & Props = {
      changeQuantities,
      selectedQuantities,
      hasContent: options.length > 0,
    };

    return (
      <div style={style}>
        <WrappedSelectFieldOptions {...wrappedProps}>
          {options}
        </WrappedSelectFieldOptions>
      </div>
    );
  };
