import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';

interface SearchBoxProps {
  value: string;
  onUpdateSearch: (event) => void;
}

export const SearchBox = (props: SearchBoxProps) => {
  const {value, onUpdateSearch} = props;
  return (
    <div className="SearchBox">
      <input type="textfield" className="SearchBox-input" value={value} onChange={onUpdateSearch}/>
      <ActionSearch style={{position: 'absolute', right: '12px', top: '5px', color: '#7b7b7b'}}/>
    </div>
  );
};
