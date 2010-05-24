[#if complex.sector??]
  [#if print]
    [#assign columns=6]
  [#else]
    [#assign columns=8]
  [/#if]
[#else]
  [#if print]
    [#assign columns=5]
  [#else]
    [#assign columns=7]
  [/#if]
[/#if]

<h2>
  <a href="file://toggleShowingShoppingList">
    [#if !print]
      <img src="../images/[#if complex.showingShoppingList]expanded[#else]collapsed[/#if].png" border="0" width="16" height="14" />
    [/#if]
    [@message key="complex.shoppingList" /]
  </a>
</h2>
[#if complex.showingShoppingList || print]
  [#if !complex.sector?? && !print]
    <p class="indent">      
      [@message key="complex.noNearestManufacturer" /]
    </p>
  [/#if]
  <table class="indent">
    <tr>
      <th class="ware">[@message key="complex.factory" /]</th>
      <th class="race">[@message key="complex.race" /]</th>
      <th class="quantity">[@message key="complex.quantity" /]</th>
      [#if !print]
        <th class="quantity">
          <table class="layout"><tr>
            <td>[@message key="complex.builtQuantity" /]&nbsp;&nbsp;</td>
            <td><img src="../images/blank.png" width="12" height="12" /></td>          
            <td><img src="../images/blank.png" width="12" height="12" /></td>
          </tr></table>
        </th>          
        <th class="quantity">[@message key="complex.leftQuantity" /]</th>
      [/#if]
      <th class="volume">[@message key="complex.factoryVolume" /]</th>
      <th class="volume">[@message key="complex.totalFactoryVolume" /]</th>
      [#if complex.sector??]
        <th class="manufacturer">[@message key="complex.nearestManufacturer" /]</th>
      [/#if]
    </tr>
    <tr>
      <td colspan="${columns}" class="sep"<img src="../images/blank.png" width="1" height="1" /></td>
    </tr>
    [#list complex.shoppingList.items as item]
      [#if item_index %2 == 0]
        [#assign class="even" /]
      [#else]
        [#assign class="odd" /]
      [/#if]
      <tr class="${class}">
        <td class="factory">${item.factory}</td>
        <td class="race">${item.factory.race}</td>
        <td class="quantity">${item.quantity}</td>
        [#if !print]
          <td class="quantity">
            [#if !print]
              <table class="layout"><tr>
                <td>${item.quantityBuilt}&nbsp;&nbsp;</td>
                <td><a href="file://buildFactory/${item.factory.id}"><img src="../images/up12.png" border="0" width="12" height="12" /></a></td>
                <td><a href="file://destroyFactory/${item.factory.id}"><img src="../images/down12.png" border="0" width="12" height="12" /></a></td>
              </tr></table>
            [#else]
              ${item.quantityBuilt}
            [/#if]
          </td>
          <td class="quantity">${item.quantityLeft}</td>
        [/#if]
        <td class="volume">${item.volume}</td>
        <td class="volume">${item.totalVolume}</td>
        [#if complex.sector??]
          <td class="manufacturer">${item.nearestManufacturer.sector}</td>
        [/#if]
      </tr>
    [/#list]
    [#if complex.shoppingList.kitQuantity > 0]
      <tr>
        <td colspan="${columns}" class="sep"><img src="../images/blank.png" width="1" height="1" /></td>
      </tr>
      [#if complex.shoppingList.items?size % 2 == 0]
        [#assign class="even" /]
      [#else]
        [#assign class="odd" /]
      [/#if]
      <tr class="${class}">
        <td class="factory" colspan="2">[@message key="complex.kit" /]</td>
        <td class="quantity">${complex.shoppingList.kitQuantity}</td>
        [#if !print]
          <td class="quantity">
            [#if !print]
              <table class="layout"><tr>
                <td>${complex.shoppingList.kitQuantityBuilt}&nbsp;&nbsp;</td>
                <td><a href="file://buildKit"><img src="../images/up12.png" border="0" width="12" height="12" /></a></td>
                <td><a href="file://destroyKit"><img src="../images/down12.png" border="0" width="12" height="12" /></a></td>
              </tr></table>
            [#else]
              ${complex.shoppingList.kitQuantityBuilt}
            [/#if]
          </td>
          <td class="quantity">${complex.shoppingList.kitQuantityLeft}</td>
        [/#if]
        <td class="volume">${complex.shoppingList.kitVolume}</td>
        <td class="volume">${complex.shoppingList.totalKitVolume}</td>
        [#if complex.sector??]            
          <td class="manufacturer">
            [#if complex.shoppingList.nearestShipyard??]
              ${complex.shoppingList.nearestShipyard}
            [/#if]
          </td>
        [/#if]
      </tr>
    [/#if]
    <tr>
      <td colspan="${columns}" class="sep"><img src="../images/blank.png" width="1" height="1" /></td>
    </tr>
    <tr>
      <th class="factory">[@message key="complex.total" /]</th>
      <td></td>
      <td class="quantity">${complex.shoppingList.totalQuantity}</td>
      [#if !print]          
        <td class="quantity">
          <table class="layout"><tr>
            <td>${complex.shoppingList.totalQuantityBuilt}&nbsp;&nbsp;</td>
            <td><img src="../images/blank.png" width="12" height="12" /></td>          
            <td><img src="../images/blank.png" width="12" height="12" /></td>
          </tr></table>
        </td>          
        <td class="quantity">${complex.shoppingList.totalQuantityLeft}</td>
      [/#if]          
      <td></td>      
      <td class="volume">${complex.shoppingList.totalVolume}</td>
      [#if complex.sector??]
      <td></td>
      [/#if]      
    </tr>        
  </table>
[/#if]